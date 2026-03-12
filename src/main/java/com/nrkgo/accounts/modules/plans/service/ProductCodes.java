package com.nrkgo.accounts.modules.plans.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for all product codes.
 * When you add a new product:
 * 1. Add an int constant here
 * 2. Add its slug + INSERT into products table in its SQL file
 * 3. Add a case in PlanServiceFactory
 */
public final class ProductCodes {

    public static final int SNAP_STEPS = 1;
    public static final int ECHO = 2;

    private ProductCodes() {
    }
}
