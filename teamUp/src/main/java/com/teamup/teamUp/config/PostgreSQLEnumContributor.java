package com.teamup.teamUp.config;

import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.service.ServiceRegistry;

public class PostgreSQLEnumContributor implements TypeContributor {
    @Override
    public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        typeContributions.contributeType(new PostgreSQLEnumType());
    }
}

