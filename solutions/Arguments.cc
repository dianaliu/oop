#include <iostream>
#include "ptr.h"
#include "java_lang.h"

namespace java {
  namespace lang {

    // Forward declaration of datalayout and vt
    struct __Arguments;
    struct __Arguments_VT;

    typedef __rt::Ptr<__Arguments> Arguments;

    // The data layout for Arguments
    struct __Arguments {
        __Arguments_VT* __vptr;

        // Constructor
        __Arguments();
        // Destructor
        static void __delete(__Arguments*);

        static int32_t hashCode(Arguments);
        static bool equals(Arguments, Object);
        static Class getClass(Arguments);
        static String toString(Arguments);
        static String retS(String, String, Arguments);
        static __Arguments_VT __vtable;

      static Class __class();

    };

    // The vtable layout for Arguments
    struct __Arguments_VT {
        Class __isa;
        void (*__delete)(__Arguments*);
        int32_t (*hashCode)(Arguments);
        bool (*equals)(Arguments, Object);
        Class (*getClass)(Arguments);
        String (*toString)(Arguments);
        String (*retS)(String, String, Arguments);
        
        __Arguments_VT():           
           __isa(__Object::__class()), 
          __delete((void(*)(__Arguments*))&__Object::__delete), 
          hashCode((int32_t(*)(Arguments))&__Object::hashCode), 
          equals((bool(*)(Arguments, Object))&__Object::equals), 
          getClass((Class(*)(Arguments))&__Object::getClass), 
          toString((String(*)(Arguments))&__Object::toString), 
          retS(&__Arguments::retS) {
        }
    };

    // The destructor
    void __Arguments::__delete(__Arguments* __this) {
      delete __this;
    }

    // Empty constructors for class and vt
      __Arguments::__Arguments() :  __vptr(&__vtable) {
      }

      __Arguments_VT __Arguments::__vtable;
  }
}


namespace __rt {
}

// ------------ begin CC file --------------
// #include <iostream>
// #include "java_lang.h"
using namespace java::lang;


String __Arguments::retS (String s, String t, Arguments __this) {
  return s;
}

int32_t main() {
  Arguments d = new __Arguments();
  String boo = __rt::literal("boo");
  // __rt::checkNotNull(d);
  // __rt::checkNotNull(boo);
  // __rt::checkNotNull(d);

  std::cout
     << __rt::literal("d.retS(s) = ") << d->__vptr->retS(__rt::literal("hi"), boo, d)
     << std::endl;
}
