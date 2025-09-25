package com.javabruse.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "photo_metadata", schema = "master_schema")
public class PhotoMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "address")
    private String address;   // Адрес объекта

    @Column(name = "latitude")
    private Double latitude;  // Широта

    @Column(name = "longitude")
    private Double longitude; // Долгота

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id")
    private Photo photo;
}
