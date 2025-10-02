using AiRise.Models.FoodDiary;
using AiRise.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace AiRise.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/diary")]
    public class FoodDiaryController : ControllerBase
    {
        private readonly FoodDiaryService _foodDiaryService;

        public FoodDiaryController(FoodDiaryService foodDiaryService)
        {
            _foodDiaryService = foodDiaryService;
        }

        [HttpGet("{firebaseUid}/{year}/{month}")]
        public async Task<IActionResult> GetMonth(string firebaseUid, int year, int month, CancellationToken ct)
        {
            try
            {
                var monthData = await _foodDiaryService.GetMonthAsync(firebaseUid, year, month, ct);
                return Ok(monthData);
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPost("{firebaseUid}/{year}/{month}/{day}/meal/{meal}")]
        public async Task<IActionResult> AddFoodEntry(string firebaseUid, int year, int month, int day, string meal, [FromBody] FoodEntry entry, CancellationToken ct)
        {
            if (entry.Calories < 0 || string.IsNullOrWhiteSpace(entry.Name))
                return BadRequest("Invalid input: Calories must be >= 0 and Name is required.");

            await _foodDiaryService.AddFoodEntryAsync(firebaseUid, year, month, day, meal, entry, ct);
            return NoContent();
        }

        [HttpPatch("{firebaseUid}/items/{entryId}")]
        public async Task<IActionResult> EditFoodEntry(string firebaseUid, string entryId, [FromBody] FoodEntry updatedEntry, CancellationToken ct)
        {
            if (updatedEntry.Calories < 0 || string.IsNullOrWhiteSpace(updatedEntry.Name))
                return BadRequest("Invalid input: Calories must be >= 0 and Name is required.");

            try
            {
                await _foodDiaryService.EditFoodEntryByIdAsync(firebaseUid, entryId, updatedEntry, ct);
                return NoContent();
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(ex.Message);
            }
        }

        [HttpDelete("{firebaseUid}/items/{entryId}")]
        public async Task<IActionResult> DeleteFoodEntry(string firebaseUid, string entryId, CancellationToken ct)
        {
            try
            {
                await _foodDiaryService.DeleteFoodEntryByIdAsync(firebaseUid, entryId, ct);
                return NoContent();
            }
            catch (KeyNotFoundException ex)
            {
                return NotFound(ex.Message);
            }
        }
    }
}