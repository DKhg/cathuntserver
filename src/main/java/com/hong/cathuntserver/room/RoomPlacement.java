package com.hong.cathuntserver.room;

import jakarta.persistence.*;

// 고양이 방 안에 가구를 어디에 뒀는지(방 꾸미기 배치). 한 가구를 여러 번 둘 수 있어 유일 제약은 없다.
@Entity
@Table(name = "room_placement")
public class RoomPlacement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "furniture_code", nullable = false, length = 32)
    private String furnitureCode;

    @Column(name = "pos_x", nullable = false)
    private float posX;

    @Column(name = "pos_y", nullable = false)
    private float posY;

    @Column(name = "flip_x", nullable = false)
    private boolean flipX;      // 좌우 반전

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;      // 겹침 순서(앞뒤)

    protected RoomPlacement() { }

    public RoomPlacement(Long accountId, String furnitureCode, float posX, float posY, boolean flipX, int sortOrder) {
        this.accountId = accountId;
        this.furnitureCode = furnitureCode;
        this.posX = posX;
        this.posY = posY;
        this.flipX = flipX;
        this.sortOrder = sortOrder;
    }

    // 값 읽기용 getter.
    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getFurnitureCode() { return furnitureCode; }
    public float getPosX() { return posX; }
    public float getPosY() { return posY; }
    public boolean isFlipX() { return flipX; }
    public int getSortOrder() { return sortOrder; }
}
