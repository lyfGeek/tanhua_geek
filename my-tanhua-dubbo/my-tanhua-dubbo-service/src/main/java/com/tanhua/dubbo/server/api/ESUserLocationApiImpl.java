package com.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.tanhua.dubbo.server.vo.UserLocationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(version = "2.0.0")
public class ESUserLocationApiImpl implements IUserLocationApi {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;

    @Value("${es.server.url}")
    private String esServerUrl;

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
        try {
            String url = this.esServerUrl + "user/location";
            Map<String, Object> param = new HashMap<>();
            param.put("userId", userId);
            param.put("longitude", longitude);
            param.put("latitude", latitude);
            param.put("address", address);

            String requestBody = MAPPER.writeValueAsString(param);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, httpHeaders);

            ResponseEntity<Void> responseEntity = this.restTemplate.postForEntity(url, httpEntity, Void.class);
            if (responseEntity.getStatusCodeValue() == 200) {
                return userId.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 查询用户地理位置。
     *
     * @param userId
     * @return
     */
    @Override
    public UserLocationVo queryByUserId(Long userId) {
        String url = this.esServerUrl + "user/location/" + userId;
        ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            return null;
        }
        try {
            String body = responseEntity.getBody();
            JsonNode jsonNode = MAPPER.readTree(body);

            UserLocationVo userLocationVo = new UserLocationVo();
            userLocationVo.setLatitude(jsonNode.get("location").get("lat").asDouble());
            userLocationVo.setLongitude(jsonNode.get("location").get("lon").asDouble());
            userLocationVo.setUserId(userId);
            userLocationVo.setAddress(jsonNode.get("address").asText());
            userLocationVo.setId(userId.toString());
            userLocationVo.setCreated(jsonNode.get("created").asLong());
            userLocationVo.setUpdated(jsonNode.get("updated").asLong());
            userLocationVo.setLastUpdated(jsonNode.get("lastUpdated").asLong());

            return userLocationVo;
        } catch (Exception e) {
            e.printStackTrace();
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
        String url = this.esServerUrl + "user/location/list";
        Map<String, Object> param = new HashMap<>();
        param.put("longitude", longitude);
        param.put("latitude", latitude);
        param.put("distance", range);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpEntity = new HttpEntity<>(MAPPER.writeValueAsString(param), headers);

            ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(url, httpEntity, String.class);
            if (responseEntity.getStatusCodeValue() != 200) {
                return null;
            }

            String body = responseEntity.getBody();
            ArrayNode jsonNode = (ArrayNode) MAPPER.readTree(body);

            List<UserLocationVo> result = new ArrayList<>();

            for (JsonNode node : jsonNode) {
                UserLocationVo userLocationVo = new UserLocationVo();
                userLocationVo.setLatitude(node.get("location").get("lat").asDouble());
                userLocationVo.setLongitude(node.get("location").get("lon").asDouble());
                userLocationVo.setUserId(node.get("userId").asLong());
                userLocationVo.setAddress(node.get("address").asText());
                userLocationVo.setId(userLocationVo.getUserId().toString());
                userLocationVo.setCreated(node.get("created").asLong());
                userLocationVo.setUpdated(node.get("updated").asLong());
                userLocationVo.setLastUpdated(node.get("lastUpdated").asLong());
                result.add(userLocationVo);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
