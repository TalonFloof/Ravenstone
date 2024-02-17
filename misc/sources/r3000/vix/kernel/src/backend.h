#pragma once

unsigned char TeletypeRawIn(int busID);
void TeletypeRawOut(int busID, unsigned char c);
void TeletypeStringOut(int busID, const char* s);
void WriteHDSector(int busID, int sector, void* data);