package org.mvm.rpc;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RpcMessage {
    @Getter
    @Setter
    private String value;
}
