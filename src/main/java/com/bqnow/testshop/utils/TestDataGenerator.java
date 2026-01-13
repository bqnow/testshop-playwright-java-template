package com.bqnow.testshop.utils;

import com.github.javafaker.Faker;

/**
 * Testdaten-Generator unter Verwendung von JavaFaker
 */
public class TestDataGenerator {
    private static final Faker faker = new Faker();

    public static class CustomerData {
        public final String firstName;
        public final String lastName;
        public final String email;
        public final String address;
        public final String city;
        public final String zipCode;

        public CustomerData() {
            this.firstName = faker.name().firstName();
            this.lastName = faker.name().lastName();
            this.email = faker.internet().emailAddress();
            this.address = faker.address().streetAddress();
            this.city = faker.address().city();
            this.zipCode = faker.number().digits(5);
        }

        @Override
        public String toString() {
            return String.format("%s %s <%s>", firstName, lastName, email);
        }
    }

    public static CustomerData generateCustomer() {
        return new CustomerData();
    }
}
