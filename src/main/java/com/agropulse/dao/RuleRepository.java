package com.agropulse.dao;

import com.agropulse.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleRepository extends JpaRepository<Rule, Integer> {}
