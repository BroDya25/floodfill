package sk.tuke.gamestudio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.entity.User;

@Service
public class UserServiceRestClient implements UserService {
    private final String url = "http://localhost:8080/api/user";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public boolean loginOrRegister(User user) {
        return restTemplate.getForObject(url + "/" + user.getUsername() + "/" + user.getPassword(), Boolean.class);
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported via web service");
    }
}
