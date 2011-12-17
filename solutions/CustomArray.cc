#include <iostream>
#include "ptr.h"
#include "java_lang.h"

namespace java {
  namespace lang {

    // Forward declaration of datalayout and vt
    struct __CustomArray;
    struct __CustomArray_VT;

    typedef __rt::Ptr<__CustomArray> CustomArray;

    // The data layout for CustomArray
    struct __CustomArray {
        __CustomArray_VT* __vptr;

        // Constructor
        __CustomArray();
        // Destructor
        static void __delete(__CustomArray*);

        static int32_t hashCode(CustomArray);
        static bool equals(CustomArray, Object);
        static Class getClass(CustomArray);
        static String toString(CustomArray);
        static __CustomArray_VT __vtable;

      static Class __class();

    };

    // The vtable layout for CustomArray
    struct __CustomArray_VT {
        Class __isa;
        void (*__delete)(__CustomArray*);
        int32_t (*hashCode)(CustomArray);
        bool (*equals)(CustomArray, Object);
        Class (*getClass)(CustomArray);
        String (*toString)(CustomArray);
        
        __CustomArray_VT():           
           __isa(__Object::__class()), 
          __delete((void(*)(__CustomArray*))&__Object::__delete), 
          hashCode((int32_t(*)(CustomArray))&__Object::hashCode), 
          equals((bool(*)(CustomArray, Object))&__Object::equals), 
          getClass((Class(*)(CustomArray))&__Object::getClass), 
          toString((String(*)(CustomArray))&__Object::toString) {
        }
    };

     // Internal accessor for java.lang.CustomArray's class.
    Class __CustomArray::__class() {
      static Class k = 
        new __Class(__rt::literal("java.lang.CustomArray"), __Object::__class());
      return k;
    }

    // The destructor
    void __CustomArray::__delete(__CustomArray* __this) {
      delete __this;
    }

    // Empty constructors for class and vt
      __CustomArray::__CustomArray() :  __vptr(&__vtable) {
      }

      __CustomArray_VT __CustomArray::__vtable;
  }
}


namespace __rt {
   // Template specialization for array of CustomArray
  template<>
  java::lang::Class Array <java::lang::CustomArray>::__class() {
    static java::lang::Class k = 
      new java::lang::__Class(literal("[Ljava.lang.CustomArray;"),
        java::lang::__Object::__class(),
        java::lang::__CustomArray::__class());
    return k;
  }
}

// ------------ begin CC file --------------
// #include <iostream>
// #include "java_lang.h"
using namespace java::lang;


int32_t main() {
  __rt::Ptr<__rt::Array<CustomArray > > c = new __rt::Array<CustomArray >(2);

  for (int32_t i = 0; i < c->length; i++) {
    (*c)[i] = new __CustomArray();
  }
  // __rt::checkNotNull(c);
  std::cout
     << __rt::literal("Class of c[0] = ") << c->__vptr->getClass(c)->__vptr->toString( c->__vptr->getClass(c))
     << std::endl;
}
