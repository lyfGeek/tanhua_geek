package com.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.pojo.UserLocation;
import com.tanhua.dubbo.server.vo.UserLocationVo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service(version = "1.0.0")
public class UserLocationApiImpl implements IUserLocationApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 更新用户地理位置。
     *
     * @param userId
     * @param longitude
     * @param latitude
     * @param address
     * @return
     */
    @Override
    public String updateUserLocation(Long userId, Double longitude, Double latitude, String address) {
        UserLocation userLocation = new UserLocation();
        userLocation.setAddress(address);
        userLocation.setUserId(userId);
        userLocation.setLocation(new GeoJsonPoint(longitude, latitude));

        // 查询用户的地理位置。
        Query query = Query.query(Criteria.where("userId").is(userId));
        UserLocation ul = this.mongoTemplate.findOne(query, UserLocation.class);
        if (null == ul) {
            // 新增用户位置数据。
            userLocation.setId(ObjectId.get());
            userLocation.setCreated(System.currentTimeMillis());
            userLocation.setUpdated(userLocation.getCreated());
            userLocation.setLastUpdated(userLocation.getCreated());

            this.mongoTemplate.save(userLocation);

            return userLocation.getId().toHexString();
        } else {
            // 更新。
            Update update = Update.update("location", userLocation.getLocation())
                    .set("updated", System.currentTimeMillis())
                    .set("lastUpdated", ul.getUpdated())
                    .set("address", userLocation.getAddress());
            this.mongoTemplate.updateFirst(query, update, UserLocation.class);
        }

        return ul.getId().toHexString();
    }

    /**
     * 查询用户地理位置。
     *
     * @param userId
     * @return
     */
    @Override
    public UserLocationVo queryByUserId(Long userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        UserLocation userLocation = this.mongoTemplate.findOne(query, UserLocation.class);
        if (null != userLocation) {
            return UserLocationVo.format(userLocation);
        }

        return null;
    }

    /**
     * 根据地理位置查询用户。
     *
     * @param longitude
     * @param latitude
     * @return
     */
    @Override
    public List<UserLocationVo> queryUserFromLocation(Double longitude, Double latitude, Integer range) {
        // 根据传入的坐标，进行确定中心点。
        GeoJsonPoint geoJsonPoint = new GeoJsonPoint(longitude, latitude);

        // 画圈的半径。
        Distance distance = new Distance(range / 1000, Metrics.KILOMETERS);

        // 画了一个圆圈。
        Circle circle = new Circle(geoJsonPoint, distance);

        Query query = Query.query(Criteria.where("location").withinSphere(circle));

        List<UserLocation> userLocations = this.mongoTemplate.find(query, UserLocation.class);

        return UserLocationVo.formatToList(userLocations);
    }

}
