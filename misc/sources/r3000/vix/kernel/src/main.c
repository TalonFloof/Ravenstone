#include "multiplexing.h"
#include "user.h"

void kmain() {
    UserInit();
    MultiInit();
    for(;;) {}
}

void kcall() {

}