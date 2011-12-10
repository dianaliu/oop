#include <iostream>
#include "ptr.h"
#include "java_lang.h"

namespace java {
  namespace lang {

    // Forward declaration of datalayout and vt
    struct __MediumTest;
    struct __MediumTest_VT;

    typedef __rt::Ptr<__MediumTest> MediumTest;

    // The data layout for MediumTest
    struct __MediumTest {
        __MediumTest_VT* __vptr;

        int32_t field;
        // Constructor
        __MediumTest();
        // Destructor
        static void __delete(__MediumTest*);

        static int32_t hashCode(MediumTest);
        static bool equals(MediumTest, Object);
        static Class getClass(MediumTest);
        static String toString(MediumTest);
        static int32_t number(MediumTest);
        static MediumTest setNumber(MediumTest, int32_t);
        static __MediumTest_VT __vtable;

      static Class __class();

    };

    // The vtable layout for MediumTest
    struct __MediumTest_VT {
        Class __isa;
        void (*__delete)(__MediumTest*);
        int32_t (*hashCode)(MediumTest);
        bool (*equals)(MediumTest, Object);
        Class (*getClass)(MediumTest);
        String (*toString)(MediumTest);
        int32_t (*number)(MediumTest);
        MediumTest (*setNumber)(MediumTest, int32_t);
        
        __MediumTest_VT():           
           __isa(__Object::__class()), 
          __delete((void(*)(__MediumTest*))&__Object::__delete), 
          hashCode((int32_t(*)(MediumTest))&__Object::hashCode), 
          equals((bool(*)(MediumTest, Object))&__Object::equals), 
          getClass((Class(*)(MediumTest))&__Object::getClass), 
          toString((String(*)(MediumTest))&__Object::toString), 
          number(&__MediumTest::number), 
          setNumber(&__MediumTest::setNumber) {
        }
    };

    // The destructor
    void __MediumTest::__delete(__MediumTest* __this) {
      delete __this;
    }

    // Empty constructors for class and vt
      __MediumTest::__MediumTest() :  __vptr(&__vtable) {
      }

      __MediumTest_VT __MediumTest::__vtable;
  }
}


namespace __rt {
}

// ------------ begin CC file --------------
// #include <iostream>
// #include "java_lang.h"
using namespace java::lang;

int32_t __MediumTest::number (MediumTest __this) {
  return __this->field;
}

MediumTest __MediumTest::setNumber (MediumTest __this, int32_t newNum) {
  // __rt::checkNotNull(field);
  // __rt::checkNotNull(newNum);
 // newNum);
__this->field = newNum;
  return __this;
}

int32_t main() {
  MediumTest thing1 = new __MediumTest();
   __rt::checkNotNull(thing1);

  thing1->__vptr->setNumber(thing1, 30);
  std::cout
     << __rt::literal("It works!")
     << std::endl;
   __rt::checkNotNull(thing1);
  std::cout
     << thing1->__vptr->number(thing1)
     << std::endl;
   __rt::checkNotNull(thing1);
  std::cout
     << thing1->__vptr->setNumber(thing1, 30)->__vptr->setNumber(thing1, 40)->__vptr->number(thing1)
     << std::endl;
}
