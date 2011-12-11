#include <iostream>
#include "ptr.h"
#include "java_lang.h"

namespace java {
  namespace lang {

    // Forward declaration of datalayout and vt
    struct __Constructor;
    struct __Constructor_VT;

    typedef __rt::Ptr<__Constructor> Constructor;

    // The data layout for Constructor
    struct __Constructor {
        __Constructor_VT* __vptr;

        int32_t field;
        // Constructor
        __Constructor();
        // Destructor
        static void __delete(__Constructor*);
      //        Constructor();

        static int32_t hashCode(Constructor);
        static bool equals(Constructor, Object);
        static Class getClass(Constructor);
        static String toString(Constructor);
        static __Constructor_VT __vtable;

      static Class __class();

    };

    // The vtable layout for Constructor
    struct __Constructor_VT {
        Class __isa;
        void (*__delete)(__Constructor*);
        int32_t (*hashCode)(Constructor);
        bool (*equals)(Constructor, Object);
        Class (*getClass)(Constructor);
        String (*toString)(Constructor);
        
        __Constructor_VT():           
           __isa(__Object::__class()), 
          __delete((void(*)(__Constructor*))&__Object::__delete), 
          hashCode((int32_t(*)(Constructor))&__Object::hashCode), 
          equals((bool(*)(Constructor, Object))&__Object::equals), 
          getClass((Class(*)(Constructor))&__Object::getClass), 
          toString((String(*)(Constructor))&__Object::toString) {
        }
    };

    // The destructor
    void __Constructor::__delete(__Constructor* __this) {
      delete __this;
    }

    // Empty constructors for class and vt
    __Constructor::__Constructor() :  __vptr(&__vtable) {
      field = 0;
      }

      __Constructor_VT __Constructor::__vtable;
  }
}


namespace __rt {
}

// ------------ begin CC file --------------
// #include <iostream>
// #include "java_lang.h"
using namespace java::lang;

int32_t main() {
  Constructor c = new __Constructor();
   __rt::checkNotNull(c);

  std::cout
     << c->field
     << std::endl;
}
