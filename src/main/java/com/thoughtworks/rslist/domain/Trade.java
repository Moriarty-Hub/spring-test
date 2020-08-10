package com.thoughtworks.rslist.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Trade {
    private int amount;
    private int rank;
}
