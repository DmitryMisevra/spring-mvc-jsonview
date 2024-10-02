package ru.javacode.springmvcjsonview.view;

public class Views {
    public static class UserSummary {
    }

    public static class UserDetails extends UserSummary {
    }
    public interface OrderSummary {}
    public interface OrderDetails extends OrderSummary {}
}
