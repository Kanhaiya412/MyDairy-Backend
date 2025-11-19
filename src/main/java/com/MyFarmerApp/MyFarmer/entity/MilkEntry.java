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

    // Relationship with User (Farmer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "U_DAY", nullable = false)
    private String day;

    @Column(name = "U_DATE", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "U_SHIFT", nullable = false)
    private Shift shift; // MORNING or EVENING

    @Column(name = "U_MILKQUANTITY", nullable = false)
    private Double milkQuantity; // in liters

    @Column(name = "U_FAT", nullable = false)
    private Double fat;

    @Column(name = "U_FAT_PRICE", nullable = false)
    private Double fatPrice; // per liter rate

    @Column(name = "U_TOTALPAYMENT", nullable = false)
    private Double totalPayment; // Calculated value
}
