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
        User u = new User("pippo", "pass123", "pippo", "verdi");
        assertTrue(service.registerUser(u));
        assertTrue(service.isUsernameExists("pippo"));
    }

    @Test
    void testRegisterUserNull() {
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(null));
    }

    @Test
    void testRegisterUserExistingUsername() {
        service.registerUser(new User("pippo", "pass123", "pippo", "verdi"));
        assertThrows(IllegalArgumentException.class,
                     () -> service.registerUser(new User("pippo", "pass456", "Mario", "Bianchi")));
    }

    @Test
    void testRegisterUserShortPassword() {
        assertThrows(IllegalArgumentException.class,
                     () -> service.registerUser(new User("ab", "pwd", "A", "B")));
    }

    @Test
    void testAuthenticateUserSuccess() {
        service.registerUser(new User("pippo", "secret", "pippo", "verdi"));
        User auth = service.authenticateUser("pippo", "secret");
        assertNotNull(auth);
        assertEquals("pippo", auth.getUsername());
        assertNull(auth.getPassword());
        assertEquals("pippo", auth.getName());
        assertEquals("verdi", auth.getSurname());
    }

    @Test
    void testAuthenticateUserWrongPassword() {
        service.registerUser(new User("pippo", "secret", "pippo", "verdi"));
        assertNull(service.authenticateUser("pippo", "wrong"));
    }

    @Test
    void testAuthenticateUserUnknownOrEmpty() {
        assertNull(service.authenticateUser("noone", "pass"));
        assertNull(service.authenticateUser(null, "pass"));
        assertNull(service.authenticateUser("pippo", ""));
    }
}