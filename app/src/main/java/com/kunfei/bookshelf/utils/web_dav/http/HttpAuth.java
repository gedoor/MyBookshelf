package com.kunfei.bookshelf.utils.web_dav.http;

public class HttpAuth {

    private static Auth auth;

    private HttpAuth() {
    }

    public static void setAuth(String user, String password) {
        auth = new Auth(user, password);
    }

    public static Auth getAuth() {
        return auth;
    }

    public static class Auth {
        private String user;
        private String pass;

        Auth(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }

        public String getUser() {
            return user;
        }

        public String getPass() {
            return pass;
        }
    }

}