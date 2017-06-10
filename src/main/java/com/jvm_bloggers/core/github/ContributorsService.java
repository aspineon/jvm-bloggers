package com.jvm_bloggers.core.github;

import com.jvm_bloggers.GithubClient;
import com.jvm_bloggers.entities.github.Contributor;
import javaslang.collection.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

@Service
public class ContributorsService {

    private static final GenericType<java.util.List<Contributor>> CONTRIBUTORS_LIST_TYPE =
        new GenericType<java.util.List<Contributor>>() {
        };

    private final Client client;
    private final GithubProperties properties;

    public ContributorsService(@GithubClient Client client, GithubProperties githubProperties) {
        this.client = client;
        this.properties = githubProperties;
    }

    @Cacheable("contributors")
    public List<Contributor> fetchContributors() {
        WebTarget target = client
            .target("{api_url}/repos/{org}/{repo}/contributors")
            .resolveTemplate("api_url", properties.getApiUrl(), false)
            .resolveTemplate("org", properties.getOrg(), false)
            .resolveTemplate("repo", properties.getRepo(), false);
        return List.ofAll(getTop30Contributors(target).collect(Collectors.toList()));
    }

    private Stream<Contributor> getTop30Contributors(WebTarget target) {
        Response response = target.request().get();
        return response.readEntity(CONTRIBUTORS_LIST_TYPE).stream().limit(30);
    }
}
