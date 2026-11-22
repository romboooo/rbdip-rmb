package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomerNameTest {

    @Test
    void splitsFullNameOnceAndPreservesRemainingNameParts() {
        Customer customer = new Customer("  Ivan Sergeevich Petrov  ", "Moscow", null);

        assertThat(customer.getFirstName()).isEqualTo("Ivan");
        assertThat(customer.getLastName()).isEqualTo("Sergeevich Petrov");
        assertThat(customer.getFullName()).isEqualTo("Ivan Sergeevich Petrov");
    }

    @Test
    void keepsSingleWordNameWithEmptyLastName() {
        Customer customer = new Customer("Cher", null, null);

        assertThat(customer.getFirstName()).isEqualTo("Cher");
        assertThat(customer.getLastName()).isEmpty();
    }
}
