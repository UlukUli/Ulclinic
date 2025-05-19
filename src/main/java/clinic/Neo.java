package clinic;
class Alo {
    protected String name;
    public Alo(String name) {
    this.name = name;
}
public void showR() {
        System.out.println(name + " cотрудник клиники");
}

}
class Nur extends Alo {
    public Nur(String name) {
    super(name);
}
public void taskA() {
        System.out.println("Помогает врачу");
    }
}
public class Neo {
    public static void main(String[] args) {
        Nur nurse = new Nur("Маша");
        nurse.showR();
        nurse.taskA();
    }
}