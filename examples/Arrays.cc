#include <iostream>
#include "ptr.h"
#include "java_lang.h"

namespace java {
  namespace lang {

    // Forward declaration of datalayout and vt
    struct __Demo;
    struct __Demo_VT;

    typedef __rt::Ptr<__Demo> Demo;

    // The data layout for Demo
    struct __Demo {

      __Demo_VT* __vptr;
      // Constructor
      __Demo();
      // Destructor
      static void __delete(__Demo*);
      
      static int32_t hashCode(Demo);
      static bool equals(Demo, Object);
      static Class getClass(Demo);
      static String toString(Demo);
      static String retS(String);
      static int32_t retI(int32_t);
      static __Demo_VT __vtable;
      
      static Class __class();

    };

    // The vtable layout for Demo
    struct __Demo_VT {
        Class __isa;
        void (*__delete)(__Demo*);
        int32_t (*hashCode)(Demo);
        bool (*equals)(Demo, Object);
        Class (*getClass)(Demo);
        String (*toString)(Demo);
        String (*retS)(String);
        int32_t (*retI)(int32_t);
        
        __Demo_VT():           
           __isa(__Object::__class()), 
          __delete((void(*)(__Demo*))&__Object::__delete), 
          hashCode((int32_t(*)(Demo))&__Object::hashCode), 
          equals((bool(*)(Demo, Object))&__Object::equals), 
          getClass((Class(*)(Demo))&__Object::getClass), 
          toString((String(*)(Demo))&__Object::toString), 
          retS((String(*)(String))&__Demo::retS), 
          retI((int32_t(*)(int32_t))&__Demo::retI) {
        }
    };

    // Empty constructors for class and vt
    __Demo::__Demo() :  __vptr(&__vtable) {
    }
    
    __Demo_VT __Demo::__vtable;


    // Internal accessor for java.lang.Demo's class.
    Class __Demo::__class() {
      static Class k =
        new __Class(__rt::literal("java.lang.Demo"), __Object::__class());
      return k;
    }

    
  }
}


namespace __rt {

     // Template specialization for array of  Demo
    template<>
    java::lang::Class Array <java::lang::Demo>::__class() {
      static java::lang::Class k = 
        new java::lang::__Class(literal("[Ljava.lang.Demo;"),
				Array<java::lang::Object>::__class(),
				java::lang::__Demo::__class());
      return k;
    }
  
}

// ------------ begin CC file --------------
// #include <iostream>
// #include "java_lang.h"
using namespace java::lang;


String __Demo::retS (String s) {
  return s;
}

int32_t __Demo::retI (int32_t i) {
  return i + 2;
}

int32_t main() {
  Demo d = new __Demo();
  String s = __rt::literal("soo");
   __rt::checkNotNull(d);
   __rt::checkNotNull(s);

  std::cout
     << __rt::literal("d.retS(s) = ") << d->__vptr->retS(s)
     << std::endl;
   __rt::checkNotNull(d);
  std::cout
     << __rt::literal("d.retI(4) = ") << d->__vptr->retI(4)
     << std::endl;
   __rt::checkNotNull(d);
   __rt::checkNotNull(s);
  std::cout
     << __rt::literal("d.retS(s).toString() = ") << d->__vptr->retS(s)->__vptr->toString(d->__vptr->retS(s))
     << std::endl;

  Object o = d;
   __rt::checkNotNull(o);

  std::cout
     << __rt::literal("o class = ") << o->__vptr->getClass(o)->__vptr->toString(o->__vptr->getClass(o))
     << std::endl;

  __rt::Ptr<__rt::Array<String> > ss = new __rt::Array<String>(2);
  __rt::Ptr<__rt::Array<Object> > oo = new __rt::Array<Object>(2);
  __rt::Ptr<__rt::Array<int32_t> > ii = new __rt::Array<int32_t>(2);

  (*ss)[0] = __rt::literal("zero");
   __rt::checkNotNull(s);
     __rt::checkStore(ss,s);
(*ss)[1] = s;
  for (int32_t i = 0; i < 2; i++) {
    std::cout
       << __rt::literal("ss = ") << (*ss)[i]
       << std::endl;
  }
  std::cout
     << __rt::literal("ss[1] = ") << (*ss)[1]
     << std::endl;
   __rt::checkNotNull(s);
  std::cout
     << __rt::literal("A string s = ") << s
     << std::endl;

  // Arrays must be 
  __rt::Ptr<__rt::Array<Demo> > dd = new __rt::Array<Demo>(2);

  (*dd)[0] = new __Demo();
  (*dd)[1] = new __Demo();

  bool f = false;

  for (int32_t i = 0; i < 2; i++) {
    std::cout
       << __rt::literal("dd[] = ") << (*dd)[i]->__vptr->getClass((*dd)[i])->__vptr->toString((*dd)[i]->__vptr->getClass((*dd)[i]))
       << std::endl;
  }
}
