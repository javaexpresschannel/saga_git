package com.je.ProductService.command.api.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,String> {
	Product findByProductId(String productId);
	Product findByProductIdOrName(String productId,String name);
}
