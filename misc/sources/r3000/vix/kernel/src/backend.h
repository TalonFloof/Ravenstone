#pragma once

unsigned char TeletypeRawIn(int busID);
void TeletypeRawOut(int busID, unsigned char c);
void TeletypeStringOut(int busID, const char* s);
int SendDisketteCommand(int diskID, int track, int sector, int cmd);
void WriteHDSector(int busID, int sector, void* data);