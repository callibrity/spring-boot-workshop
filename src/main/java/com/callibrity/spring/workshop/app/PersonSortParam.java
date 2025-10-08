package com.callibrity.spring.workshop.app;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jwcarman.jpa.pagination.SortPropertyProvider;

@RequiredArgsConstructor
@Getter
public enum PersonSortParam implements SortPropertyProvider {

    FIRST_NAME("firstName"),
    LAST_NAME("lastName");

    private final String sortProperty;
}
