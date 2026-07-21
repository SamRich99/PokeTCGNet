package it.unipi.poketcgnet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SliceDTO<T> {

    private List<T> content;
    private boolean hasNext;
    private boolean hasPrevious;
}
