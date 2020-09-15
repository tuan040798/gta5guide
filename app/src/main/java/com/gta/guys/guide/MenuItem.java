package com.gta.guys.guide;

/**
 * The {@link MenuItem} class.
 * <p>Defines the attributes for a restaurant menu item.</p>
 */
class MenuItem {

    private final String name;
    private final String imageName;
    private final String summary;
    private final String data;

    public MenuItem(String name, String imageName, String summary, String data) {
        this.name = name;
        this.imageName = imageName;
        this.summary = summary;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public String getSummary() {
        return summary;
    }

    public String getData() {
        return data;
    }
}
