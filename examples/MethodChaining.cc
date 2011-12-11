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
        static String retS(String, Demo);
        static int32_t retI(int32_t, Demo);
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
        String (*retS)(String, Demo);
        int32_t (*retI)(int32_t, Demo);
        
        __Demo_VT():           
           __isa(__Object::__class()), 
          __delete((void(*)(__Demo*))&__Object::__delete), 
          hashCode((int32_t(*)(Demo))&__Object::hashCode), 
          equals((bool(*)(Demo, Object))&__Object::equals), 
          getClass((Class(*)(Demo))&__Object::getClass), 
          toString((String(*)(Demo))&__Object::toString), 
          retS(&__Demo::retS), 
          retI(&__Demo::retI) {
        }
    };

    // The destructor
    void __Demo::__delete(__Demo* __this) {
      delete __this;
    }

    // Empty constructors for class and vt
      __Demo::__Demo() :  __vptr(&__vtable) {
      }

      __Demo_VT __Demo::__vtable;
  }
}


namespace __rt {
}

// ------------ begin CC file --------------
// #include <iostream>
// #include "java_lang.h"
using namespace java::lang;


String __Demo::retS (String s, Demo __this) {
  return s;
}

int32_t __Demo::retI (int32_t i, Demo __this) {
  return i + 2;
}

int32_t main() {
  Demo d = new __Demo();
  String s = __rt::literal("soo");
   __rt::checkNotNull(d);
   __rt::checkNotNull(s);
   __rt::checkNotNull(d);

  std::cout
     << __rt::literal("d.retS(s) = ") << d->__vptr->retS(s, d)
     << std::endl;
   __rt::checkNotNull(d);
   __rt::checkNotNull(d);
  std::cout
     << __rt::literal("d.retI(4) = ") << d->__vptr->retI(4, d)
     << std::endl;
   __rt::checkNotNull(d);
   __rt::checkNotNull(s);
   // The below line needs to be fixed
  std::cout
    << __rt::literal("d.retS(s).toString() = ") << d->__vptr->retS(s,d)->__vptr->toString(d->__vptr->retS(s, d))
     << std::endl;

  Object o = d;
   __rt::checkNotNull(o);

  std::cout
     << __rt::literal("o class = ") << o->__vptr->getClass(o)->__vptr->toString(o->__vptr->getClass(o))
     << std::endl;
}
