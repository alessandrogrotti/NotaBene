package com.google.gwt.sample.notabene.server;

import com.google.gwt.sample.notabene.shared.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class UserServiceImplTest {
    private UserServiceImpl service;
    private UserRepository repo;

    @BeforeEach
    void setUp() {
        repo = UserRepository.getInstance();
        repo.clear();
        service = new UserServiceImpl();
    }

    @Test
    void testRegisterUserSuccess() {
        User u = new User("mario", "pass123", "Mario", "Rossi");
        assertTrue(service.registerUser(u));
        assertTrue(service.isUsernameExists("mario"));
    }

    @Test
    void testRegisterUserNull() {
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(null));
    }

    @Test
    void testRegisterUserExistingUsername() {
        service.registerUser(new User("mario", "pass123", "Mario", "Rossi"));
        assertThrows(IllegalArgumentException.class,
                     () -> service.registerUser(new User("mario", "pass456", "Luigi", "Bianchi")));
    }

    @Test
    void testRegisterUserShortPassword() {
        assertThrows(IllegalArgumentException.class,
                     () -> service.registerUser(new User("ab", "pwd", "A", "B")));
    }

    @Test
    void testAuthenticateUserSuccess() {
        service.registerUser(new User("mario", "secret", "Mario", "Rossi"));
        User auth = service.authenticateUser("mario", "secret");
        assertNotNull(auth);
        assertEquals("mario", auth.getUsername());
        assertNull(auth.getPassword());
        assertEquals("Mario", auth.getName());
        assertEquals("Rossi", auth.getSurname());
    }

    @Test
    void testAuthenticateUserWrongPassword() {
        service.registerUser(new User("mario", "secret", "Mario", "Rossi"));
        assertNull(service.authenticateUser("mario", "wrong"));
    }

    @Test
    void testAuthenticateUserUnknownOrEmpty() {
        assertNull(service.authenticateUser("inesistente", "pass"));
        assertNull(service.authenticateUser(null, "pass"));
        assertNull(service.authenticateUser("mario", ""));
    }
}