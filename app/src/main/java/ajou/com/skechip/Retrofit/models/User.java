package ajou.com.skechip.Retrofit.models;

public class User {

    private Integer id, member;
    private Long kakaoid;
    private String name;
    private  String profileImagePath;

    public User(Integer id, Long kakaoid, String name, String profileImagePath, Integer member) {
        this.id = id;
        this.kakaoid = kakaoid;
        this.name = name;
        this.member = member;
        this.profileImagePath = profileImagePath;
    }

    public Integer getId() {
        return id;
    }

    public Long getKakaoid() {
        return kakaoid;
    }

    public String getName() {
        return name;
    }

    public Integer getMember() {
        return member;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }
}