package com.tamagochy.model;

import java.util.List;

public class User {

    private String username;
    private String email;
    private String nome;
    private String userUID;

    public User(String email, String nome, String userUID) {
        this.email = email;
        this.nome = nome;
        this.userUID = userUID;
    }

    @Override
    public String toString(){
        return this.email + " " +
                this.nome + " " +
                this.userUID + " ";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

}
