package com.ax.code.es.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author lj
 */
public class Sort implements Iterable<Sort.Order>, Serializable {
    private static final long serialVersionUID = 5737186511678863905L;
    public static final Sort.Direction DEFAULT_DIRECTION;
    private List<Order> orders;

    public Sort() {
    }

    public Sort(Sort.Order... orders) {
        this(Arrays.asList(orders));
    }

    public Sort(List<Sort.Order> orders) {
        this.orders = orders;
    }

    public Sort(String... properties) {
        this(DEFAULT_DIRECTION, properties);
    }

    public Sort(Sort.Direction direction, String... properties) {
        this(direction, (List) (properties == null ? new ArrayList() : Arrays.asList(properties)));
    }

    public Sort(Sort.Direction direction, List<String> properties) {
        if (properties != null && !properties.isEmpty()) {
            this.orders = new ArrayList(properties.size());
            Iterator var3 = properties.iterator();

            while (var3.hasNext()) {
                String property = (String) var3.next();
                this.orders.add(new Sort.Order(direction, property));
            }

        } else {
            throw new IllegalArgumentException("You have to provide at least one property to sort by!");
        }
    }

    public Sort and(Sort sort) {
        if (sort == null) {
            return this;
        } else {
            ArrayList<Sort.Order> these = new ArrayList(this.orders);
            Iterator var3 = sort.orders.iterator();

            while (var3.hasNext()) {
                Sort.Order order = (Sort.Order) var3.next();
                these.add(order);
            }

            return new Sort(these);
        }
    }

    public Sort.Order getOrderFor(String property) {
        Iterator var2 = this.orders.iterator();

        Sort.Order order;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            order = (Sort.Order) var2.next();
        } while (!order.getProperty().equals(property));

        return order;
    }

    @Override
    public Iterator<Sort.Order> iterator() {
        return this.orders.iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Sort)) {
            return false;
        } else {
            Sort that = (Sort) obj;
            return this.orders.equals(that.orders);
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.orders.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.orders.toString();
    }

    static {
        DEFAULT_DIRECTION = Sort.Direction.ASC;
    }

    public static class Order implements Serializable {
        private static final long serialVersionUID = 1522511010900108987L;
        private final Sort.Direction direction;
        private final String property;

        public Order() {
            this.direction = Sort.DEFAULT_DIRECTION;
            this.property = "";
        }

        public Order(Sort.Direction direction, String property) {
            if (property != null && !"".equals(property.trim())) {
                this.direction = direction == null ? Sort.DEFAULT_DIRECTION : direction;
                this.property = property;
            } else {
                throw new IllegalArgumentException("PropertyPath must not null or empty!");
            }
        }

        public Order(String property) {
            this(Sort.DEFAULT_DIRECTION, property);
        }

        public static List<Sort.Order> create(Sort.Direction direction, Iterable<String> properties) {
            List<Sort.Order> orders = new ArrayList();
            Iterator var3 = properties.iterator();

            while (var3.hasNext()) {
                String property = (String) var3.next();
                orders.add(new Sort.Order(direction, property));
            }

            return orders;
        }

        public Sort.Direction getDirection() {
            return this.direction;
        }

        public String getProperty() {
            return this.property;
        }

        public boolean isAscending() {
            return this.direction.equals(Sort.Direction.ASC);
        }

        public Sort.Order with(Sort.Direction order) {
            return new Sort.Order(order, this.property);
        }

        public Sort withProperties(String... properties) {
            return new Sort(this.direction, properties);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + this.direction.hashCode();
            result = 31 * result + this.property.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof Sort.Order)) {
                return false;
            } else {
                Sort.Order that = (Sort.Order) obj;
                return this.direction.equals(that.direction) && this.property.equals(that.property);
            }
        }

        @Override
        public String toString() {
            return String.format("%s: %s", this.property, this.direction);
        }
    }

    public static enum Direction {
        ASC,
        DESC;

        private Direction() {
        }

        public static Sort.Direction fromString(String value) {
            try {
                return valueOf(value.toUpperCase(Locale.US));
            } catch (Exception var2) {
                throw new IllegalArgumentException(String.format("Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), var2);
            }
        }
    }
}
