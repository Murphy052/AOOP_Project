class OldVolunteerServices {
    private final String id;
    private final String name;

    public OldVolunteerServices(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return String.format("{\"id\":\"%s\",\"name\":\"%s\"}", id, name);
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
}
