package hello.hello_spring.controller;

public class MemberForm {
    private String name; // return 을 통해 해당 클래스에 매칭될것이다.

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
