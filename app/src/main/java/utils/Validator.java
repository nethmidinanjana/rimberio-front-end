package utils;

public class Validator {
    public static boolean isValidEmail(String email){
        return email != null && email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");
    }

    public static boolean isValidContact(String contact){
        return contact != null && contact.matches("^[0]{1}[7]{1}[01245678]{1}[0-9]{7}$");
    }

}
