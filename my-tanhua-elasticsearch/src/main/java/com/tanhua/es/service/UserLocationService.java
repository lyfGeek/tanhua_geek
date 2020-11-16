package com.tanhua.es.service;

import com.tanhua.es.pojo.UserLocationES;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserLocationService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public boolean updateUserLocation(Long userId, Double longitude, Double latitude, String address) {

        try {
            // 判断索引库是否存在，如果不存在需要创建。
            if (!this.elasticsearchTemplate.indexExists(UserLocationES.class)) {
                // 创建索引。
                this.elasticsearchTemplate.createIndex(UserLocationES.class);
            }

            // 判断类型是否存在，如果不存在，需要创建。
            if (!this.elasticsearchTemplate.typeExists("tanhua", "user_location")) {
                // 创建类型。
                this.elasticsearchTemplate.putMapping(UserLocationES.class);
            }

            // 判断用户的位置数据是否存在，如果存在，进行更新操作，如果不存在，插入新的数据。
            GetQuery getQuery = new GetQuery();
            getQuery.setId(userId.toString());

            UserLocationES ul = this.elasticsearchTemplate.queryForObject(getQuery, UserLocationES.class);
            if (null == ul) {
                // 新增位置数据。
                UserLocationES userLocationES = new UserLocationES();
                userLocationES.setAddress(address);
                userLocationES.setCreated(System.currentTimeMillis());
                userLocationES.setLastUpdated(userLocationES.getCreated());
                userLocationES.setUpdated(userLocationES.getCreated());
                userLocationES.setUserId(userId);
                userLocationES.setLocation(new GeoPoint(latitude, longitude));

                IndexQuery indexQuery = new IndexQueryBuilder().withObject(userLocationES).build();
                this.elasticsearchTemplate.index(indexQuery);// 保存数据到 Elasticsearch 中。
            } else {
                // 更新操作。
                Map<String, Object> map = new HashMap<>();
                map.put("lastUpdated", ul.getUpdated());
                map.put("updated", System.currentTimeMillis());
                map.put("address", address);
                map.put("location", new GeoPoint(latitude, longitude));

                UpdateRequest updateRequest = new UpdateRequest();
                updateRequest.doc(map);

                UpdateQuery updateQuery = new UpdateQueryBuilder().withId(userId.toString())
                        .withClass(UserLocationES.class)
                        .withUpdateRequest(updateRequest).build();

                this.elasticsearchTemplate.update(updateQuery);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public UserLocationES queryByUserId(Long userId) {
        GetQuery getQuery = new GetQuery();
        getQuery.setId(userId.toString());
        return this.elasticsearchTemplate.queryForObject(getQuery, UserLocationES.class);
    }

    /**
     * 查询附近的人。
     *
     * @param longitude 经度。
     * @param latitude  纬度。
     * @param distance  距离（单位：米）。
     * @param page      页数。
     * @param pageSize  页面大小。
     * @return
     */
    public Page<UserLocationES> queryUserFromLocation(Double longitude, Double latitude, Double distance, Integer page, Integer pageSize) {

        String fieldName = "location";

        // NativeSearchQuery 实现了 SearchQuery 接口。
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        // 分页。
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        nativeSearchQueryBuilder.withPageable(pageRequest);

        // 定义 bool 查询。
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // geo 查询，定义中心点，指定查询范围。
        GeoDistanceQueryBuilder geoDistanceQueryBuilder = new GeoDistanceQueryBuilder(fieldName);
        geoDistanceQueryBuilder.point(latitude, longitude);
        geoDistanceQueryBuilder.distance(distance / 1000, DistanceUnit.KILOMETERS);

        boolQueryBuilder.must(geoDistanceQueryBuilder);
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        // 按照距离升序。
        GeoDistanceSortBuilder geoDistanceSortBuilder = new GeoDistanceSortBuilder(fieldName, latitude, longitude);
        geoDistanceSortBuilder.unit(DistanceUnit.KILOMETERS);// 距离单位。
        geoDistanceSortBuilder.order(SortOrder.ASC);// 升序。

        nativeSearchQueryBuilder.withSort(geoDistanceSortBuilder);

        return this.elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), UserLocationES.class);
    }

}
