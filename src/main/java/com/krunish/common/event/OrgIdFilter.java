package com.krunish.common.event;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@FilterDef(
        name = "orgFilter",
        parameters = @ParamDef(name = "orgId", type = Long.class)
)
public class OrgIdFilter {
}
