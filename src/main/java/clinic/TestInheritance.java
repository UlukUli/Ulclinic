package clinic;

class StaffMember {
    protected String name;

    public StaffMember(String name) {
        this.name = name;
    }

    public void showRole() {
        System.out.println(name + " — сотрудник клиники");
    }
}

class Nurse extends StaffMember {
    public Nurse(String name) {
        super(name);
    }

    public void assistDoctor() {
        System.out.println(name + " помогает врачу");
    }
}

public class TestInheritance {
    public static void main(String[] args) {
        Nurse nurse = new Nurse("Наталья");
        nurse.showRole(); // метод от StaffMember
        nurse.assistDoctor(); // метод из Nurse
    }
}