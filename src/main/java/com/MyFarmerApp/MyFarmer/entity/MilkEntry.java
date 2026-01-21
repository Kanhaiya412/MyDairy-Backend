package com.MyFarmerApp.MyFarmer.entity;

import com.MyFarmerApp.MyFarmer.enums.Shift;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "DIV_MILKENTRY")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilkEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation to User (Farmer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // NEW — Optional cattle-wise milk logging
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cattle_id")
    private CattleEntry cattleEntry;  // NULL = full farm milk entry

    @Column(name = "U_DAY", nullable = false)
    private String day;

    @Column(name = "U_DATE", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "U_SHIFT", nullable = false)
    private Shift shift;

    @Column(name = "U_MILKQUANTITY", nullable = false)
    private Double milkQuantity;

    @Column(name = "U_FAT")
    private Double fat;

    @Column(name = "U_FAT_PRICE")
    private Double fatPrice;

    @Column(name = "U_TOTALPAYMENT")
    private Double totalPayment;
}
