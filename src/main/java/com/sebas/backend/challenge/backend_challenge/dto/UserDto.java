package com.sebas.backend.challenge.backend_challenge.dto;

public class UserDto {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private String password;
    private boolean admin;

    // Getters y Setters

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public String getLastname() { 
        return lastname; 
    }
    public void setLastname(String lastname) { 
        this.lastname = lastname; 
    }

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
