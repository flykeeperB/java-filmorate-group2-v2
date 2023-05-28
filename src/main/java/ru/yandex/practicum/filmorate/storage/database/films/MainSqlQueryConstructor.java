package ru.yandex.practicum.filmorate.storage.database.films;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.storage.database.SqlQueryConstructor;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class MainSqlQueryConstructor implements SqlQueryConstructor {

    private String fieldsPart;

    private String fromPart;

    private String wherePart;

    private String groupPart;

    private String orderPart;

    private String limitPart;

    @Override
    public String getSelectQuery() {

        StringBuilder sb = new StringBuilder("SELECT");
        sb.append(this.getFieldsPart());

        if (getFromPart() != null) {
            sb.append(" FROM ");
            sb.append(getFromPart());
        }

        if (getWherePart() != null) {
            sb.append(" WHERE ");
            sb.append(getWherePart());
        }

        if (getGroupPart() != null) {
            sb.append(" GROUP BY ");
            sb.append(getGroupPart());
        }

        if (getOrderPart() != null) {
            sb.append(" ORDER BY ");
            sb.append(getOrderPart());
        }

        if (getLimitPart() != null) {
            sb.append(" LIMIT ");
            sb.append(getLimitPart());
        }

        return sb.toString();
    }
}
