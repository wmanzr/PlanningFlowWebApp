package RUT.PlanningFlow.adapter.in.web.security;

import java.io.Serializable;

public record JwtPrincipal(int userId, String username) implements Serializable {
}
