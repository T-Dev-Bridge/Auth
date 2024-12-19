package com.web.auth.service.Core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.base.base.service.CrudDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ManagerDto extends CrudDto {

    private String id;
    private String username;
    private String password;
    private String email;
    private Long pwdId;
    private Long roleId;
    private boolean enabled;

    @Override
    public Object toEntity() {
        return null;
    }
}
