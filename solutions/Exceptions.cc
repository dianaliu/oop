#include <iostream>
#include "ptr.h"
#include "java_lang.h"

namespace java {
  namespace lang {

    // Forward declaration of datalayout and vt
    struct __Exceptions;
    struct __Exceptions_VT;

    typedef __rt::Ptr<__Exceptions> Exceptions;

    // The data layout for Exceptions
    struct __Exceptions {
        __Exceptions_VT* __vptr;

        // Constructor
        __Exceptions();
        // Destructor
        static void __delete(__Exceptions*);

        static int32_t hashCode(Exceptions);
        static bool equals(Exceptions, Object);
        static Class getClass(Exceptions);
        static String toString(Exceptions);
        static __Exceptions_VT __vtable;

      static Class __class();

    };

    // The vtable layout for Exceptions
    struct __Exceptions_VT {
        Class __isa;
        void (*__delete)(__Exceptions*);
        int32_t (*hashCode)(Exceptions);
        bool (*equals)(Exceptions, Object);
        Class (*getClass)(Exceptions);
        String (*toString)(Exceptions);
        
        __Exceptions_VT():           
           __isa(__Object::__class()), 
          __delete((void(*)(__Exceptions*))&__Object::__delete), 
          hashCode((int32_t(*)(Exceptions))&__Object::hashCode), 
          equals((bool(*)(Exceptions, Object))&__Object::equals), 
          getClass((Class(*)(Exceptions))&__Object::getClass), 
          toString((String(*)(Exceptions))&__Object::toString) {
        }
    };

    // The destructor
    void __Exceptions::__delete(__Exceptions* __this) {
      delete __this;
    }

    // Empty constructors for class and vt
      __Exceptions::__Exceptions() :  __vptr(&__vtable) {
      }

      __Exceptions_VT __Exceptions::__vtable;
  }
}


namespace __rt {
}

// ------------ begin CC file --------------
// #include <iostream>
// #include "java_lang.h"
using namespace java::lang;


int32_t main() {
  try {
    throw NullPointerException();
  } catch (Exception e) {
    std::cout
       << __rt::literal("Succesful error throwing")
       << std::endl;
  } 
}
