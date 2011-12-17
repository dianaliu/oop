public class Testere {
  public static void main(String[] args) {
    Testere gokcek = new Testere();
//    
//    C a = new C();
    B b2 = new B();
//    C c = new C();
    byte by = 5;
    short a = -8;
    long b = -127;
    gokcek.orhan(b, a, b2);
//    gokcek.orhan((A)b, b);
//    gokcek.orhan(c, c);            int < long < float < double < short
  }

// void orhan(byte a1) { System.out.println("m(byte)     : "+ a1 );}
 void orhan(short a1, int a2, A a3) { System.out.println("m(short)     : "+ a1 );}

  void orhan(long a1, int a2, C a3) { System.out.println("m(long)     : "+ a1 );}
  void orhan(float a1, int a2, B a3) { System.out.println("m(float)     : "+ a1 );}
  
  
  void orhan(float a1) { System.out.println("m(float)     : "+ a1 );}
  void orhan(double a1) { System.out.println("m(double)     : "+ a1 );}
  
  
  void orhan(double a1, int a2, B a3) { System.out.println("m(long)     : "+ a1 );}


//  void orhan(B b1, B b2) { System.out.println("m(B,B)     : "+ b1 +", "+ b2);}
//    void orhan(int c1, long c2) { System.out.println("m(long,float)     : "+ c1 +", "+ c2);}
//  public void orhan(B b1, A a2) { System.out.println("m(B,A)     : "+ b1 +", "+ a2);}
//  public void orhan(C c1, C c2) { System.out.println("m(C,C)     : "+ c1 +", "+ c2);}
//
}


  class A           { public String toString() { return "A"; } }
  class B extends A { public String toString() { return "B"; } }
  class C extends B { public String toString() { return "C"; } }
  
  
/*
 class pust extends Testere{
 int yildirim(long a)        { 
 int duydum = 7;
 return duydum;
 }
 }*/ 