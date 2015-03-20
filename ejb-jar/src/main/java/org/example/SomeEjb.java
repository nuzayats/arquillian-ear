package org.example;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class SomeEjb {
    public String hello(String name) {
        return "Hello, " + name;
    }
}
