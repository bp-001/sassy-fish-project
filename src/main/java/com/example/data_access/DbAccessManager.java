package com.example.data_access;

import java.util.List;
import java.util.function.Consumer;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import com.example.usermodel.Post;
import com.example.usermodel.User;

public class DbAccessManager implements AutoCloseable {
    private final StandardServiceRegistry registry;
    protected final EntityManager db;
    protected final EntityManagerFactory emf;

    public DbAccessManager() {
        this.registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.fxml")
                .build();
        try {
            emf = new MetadataSources(registry).buildMetadata().buildSessionFactory();
            db = emf.createEntityManager();
            System.out.println("DataBase opened");
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw new RuntimeException("Error creating EntityManagerFactory: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }
    }

    public <T> T findById(Class<T> entityClass, Long id) {
        return db.find(entityClass, id);
    }

    public <T> T save(T entity) {
        return executeInTransaction(() -> {
            db.persist(entity);
            return entity;
        });
    }

    public <T> T update(T entity) {
        return executeInTransaction(() -> db.merge(entity));
    }

    public void delete(Object entity) {
        executeInTransaction(() -> {
            Object managed = db.contains(entity) ? entity : db.merge(entity);
            db.remove(managed);
            return null;
        });
    }

    public void withTransaction(Consumer<EntityManager> action) {
        executeInTransaction(() -> {
            action.accept(db);
            return null;
        });
    }

    public User saveUser(User user) {
        return save(user);
    }

    public User updateUser(User user) {
        return update(user);
    }

    public User findUserById(Long id) {
        return findById(User.class, id);
    }

    public User findUserByUsername(String username) {
        List<User> users = db.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    public Post savePost(Post post) {
        return save(post);
    }

    public Post updatePost(Post post) {
        return update(post);
    }

    public List<Post> findAllPosts() {
        return db.createQuery("SELECT p FROM Post p ORDER BY p.date DESC", Post.class)
                .getResultList();
    }

    public long countPostsByUsername(String username) {
        return db.createQuery(
                "SELECT COUNT(p) FROM Post p WHERE p.user.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    public void deletePost(Post post) {
        delete(post);
    }

    @Override
    public void close() {
        if (db.isOpen()) {
            db.close();
        }
        if (emf.isOpen()) {
            emf.close();
        }
        StandardServiceRegistryBuilder.destroy(registry);
        System.out.println("DataBase is closed");
    }

    private <T> T executeInTransaction(TransactionOperation<T> operation) {
        try {
            db.getTransaction().begin();
            T result = operation.execute();
            db.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw new RuntimeException("Database transaction failed", e);
        }
    }

    @FunctionalInterface
    private interface TransactionOperation<T> {
        T execute();
    }
}
