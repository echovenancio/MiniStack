package com.echovenancio.ministack.models;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.echovenancio.ministack.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostDto {
    private Long id;
    private String title;
    private String body;
    private String authorUsername;
    private Collection<String> tags;

    public PostDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.body = post.getBody();
        this.authorUsername = post.getUser().getUsername();
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            Set<String> tagNames = post.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toSet());
            this.tags = Collections.unmodifiableCollection(tagNames);
        } else {
            this.tags = Collections.emptyList();
        }
    }
}
