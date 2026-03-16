package com.optiplant.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_exchanges")
@Getter
@Setter
@AllArgsConstructor
public class ProductExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sale_item_id", nullable = false)
    private SaleItem saleItem;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "new_product_id", nullable = false)
    private Product newProduct;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double amountDifference;

    @Column(nullable = false)
    private Double paidAmount;

    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ProductExchange() {
        this.createdAt = LocalDateTime.now();
    }
}

