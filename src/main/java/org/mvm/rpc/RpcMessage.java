package org.mvm.rpc;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RpcMessage implements Serializable {
    @Getter
    @Setter
    private String value;
}
