package eus.ehu.usermodel;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bio;
    private String location;


    @Column(nullable = false, unique = true)
    private String username;
    private String email;
    private String profilePicturePath;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    public User() {
    }   //Default constructor for JPA  

    public User(String username, String email){
        this.username = username;
        this.email = email;
    }

        // Getters y Setters básicos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getProfilePicturePath() { return profilePicturePath; }
    public void setProfilePicturePath(String profilePicturePath) { 
        this.profilePicturePath = profilePicturePath;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts.clear();
        if (posts == null) {
            return;
        }
        for (Post post : posts) {
            addPost(post);
        }
    }

    public void addPost(Post post) {
        if (post == null || posts.contains(post)) {
            return;
        }
        posts.add(post);
        post.setUser(this);
    }

    public void removePost(Post post) {
        if (post == null) {
            return;
        }
        posts.remove(post);
        post.setUser(null);
    }

    
}

