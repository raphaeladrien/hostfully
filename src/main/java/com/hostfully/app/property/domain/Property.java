package com.hostfully.app.property.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Property {
    private String id;
    private String description;
    private String alias;
}
